import java.io.BufferedReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.imageio.ImageIO;

import org.h2.tools.SimpleResultSet;

import fc.Database.JdbcUtils.ScriptRunner;

public class Main {
    public void runSQL(String fileName) {
        // Tout nom de fichier relatif DOIT commencer par ./ avec H2, sinon le driver
        // refuse la chaine

        String connUrl = "jdbc:h2:./h2database";
        String username = "sa";
        String password = "";

        // Le fichier créé va être "h2database.mv.db" dans le repertoire de
        // l'application Java

        try (Connection conn = DriverManager.getConnection(connUrl, username, password)) {
            ScriptRunner runner = new ScriptRunner(conn, true, true);
            FileReader file = new FileReader(fileName);
            runner.runScript(new BufferedReader(file));

            System.out.println();

            try (Statement st = conn.createStatement()) {
                st.execute("SHUTDOWN");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            assertTrue(false);
        }
    }

    private void assertTrue(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assertTrue'");
    }

    public static ResultSet getColumnAverage(Connection conn, int column) throws // Récupère la moyenn d'une colonne
                                                                                 // (TP3)
    SQLException {
        SimpleResultSet rs = new SimpleResultSet();

        CustomAverage customAverage = new CustomAverage();
        customAverage.add(column);

        rs.addColumn("AverageX", Types.INTEGER, 10, 0);
        // String url = conn.getMetaData().getURL();
        // if (url.equals("jdbc:columnlist:connection")) {
        // return rs;
        // }
        // Ajoute des lignes (rangees, occurrences de la table)
        rs.addRow(customAverage.getResult());
        return rs;
    }

    public static ResultSet getMatrix(Connection conn, Integer size) throws SQLException {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("X", Types.INTEGER, 10, 0);
        rs.addColumn("Y", Types.INTEGER, 10, 0);
        // String url = conn.getMetaData().getURL();
        // if (url.equals("jdbc:columnlist:connection")) {
        // return rs;
        // }
        // Ajoute des lignes (rangees, occurrences de la table)
        for (int s = size, x = 0; x < s; x++) {
            for (int y = 0; y < s; y++) {
                rs.addRow(x, y);
            }
        }
        return rs;
    }

    // Tp6 Question 1
    public static ResultSet getGaussienne(Connection conn, Integer size) throws SQLException {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("X", Types.INTEGER, 10, 0);
        rs.addColumn("Y", Types.INTEGER, 10, 0);
        rs.addColumn("VALEUR", Types.FLOAT, 10, 0);

        for (int x = -size / 2; x <= size / 2; x++) {
            for (int y = -size / 2; y <= size / 2; y++) {
                rs.addRow(x, y, 200 * (Math.exp(-x * x - y * y)));
            }
        }
        return rs;
    }

    public static ResultSet getRGBImage(Connection conn, String path) throws SQLException, IOException {

        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("X", Types.INTEGER, 10, 0);
        rs.addColumn("Y", Types.INTEGER, 10, 0);
        rs.addColumn("R", Types.FLOAT, 10, 0);
        rs.addColumn("G", Types.FLOAT, 10, 0);
        rs.addColumn("B", Types.FLOAT, 10, 0);

        BufferedImage image = ImageIO.read(new File(path));

        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = image.getRGB(x, y);
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;
                rs.addRow(x, y, red, green, blue);
            }
        }

        return rs;
    }

    public static void gaussian(Connection conn, String tableName, int k) throws SQLException, IOException {
        String sql = "SELECT i.X, i.Y, AVG(i.R * g.VALEUR) AS R, AVG(i.G * g.VALEUR) AS G, AVG(i.B * g.VALEUR) AS B " +
                "FROM " + tableName + " i " +
                "CROSS JOIN GAUSS g " +
                "WHERE g.X BETWEEN ? AND ? AND g.Y BETWEEN ? AND ? " +
                "GROUP BY i.X, i.Y";

        BufferedImage image = ImageIO.read(new File("MyJavaProject/src/building.png"));

        int width = image.getWidth();
        int height = image.getHeight();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, -k / 2);
            stmt.setInt(2, k / 2);
            stmt.setInt(3, -k / 2);
            stmt.setInt(4, k / 2);

            ResultSet rs = stmt.executeQuery();

            BufferedImage filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            while (rs.next()) {
                int x = rs.getInt("X");
                int y = rs.getInt("Y");
                int red = rs.getInt("R");
                int green = rs.getInt("G");
                int blue = rs.getInt("B");

                int rgb = (red << 16) | (green << 8) | blue;
                filteredImage.setRGB(x, y, rgb);
            }

            ImageIO.write(filteredImage, "png", new File("gaussian_" + k + ".png"));
        }
    }

    public static void downscale(Connection conn, String inputPath, String outputPath, int scale)
            throws SQLException, IOException {
        String sql = "SELECT ROUND(AVG(R)), ROUND(AVG(G)), ROUND(AVG(B)) " +
                "FROM (SELECT R, G, B, " +
                "FLOOR(X / ?) AS X_GROUP, FLOOR(Y / ?) AS Y_GROUP " +
                "FROM RGBIMAGE(?)) " +
                "GROUP BY X_GROUP, Y_GROUP";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scale);
            stmt.setInt(2, scale);
            stmt.setString(3, inputPath);

            ResultSet rs = stmt.executeQuery();

            BufferedImage image = ImageIO.read(new File(inputPath));
            int width = image.getWidth() / scale;
            int height = image.getHeight() / scale;
            BufferedImage downscaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            int x = 0, y = 0;
            while (rs.next()) {
                int red = rs.getInt(1);
                int green = rs.getInt(2);
                int blue = rs.getInt(3);

                int rgb = (red << 16) | (green << 8) | blue;
                downscaledImage.setRGB(x, y, rgb);

                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            }

            ImageIO.write(downscaledImage, "png", new File(outputPath));
        }
    }

    public static void main(String[] args) {
        String connUrl = "jdbc:h2:./h2database";
        String username = "sa";
        String password = "";
        // Le fichier créé va être "h2database.mv.db" dans le repertoire de'application
        // Java
        try (Connection conn = DriverManager.getConnection(connUrl, username,
                password)) {
            // Votre code ici, qui utilise 'conn',
            // qui cree des statements, execute des requetes etc.

            // Création des différents Statements (correspond aux fonctions personnalisées
            // SQL)
            Statement stat = conn.createStatement();
            stat.execute("CREATE ALIAS MATRIX FOR \"Main.getMatrix\"");

            Statement averageStat = conn.createStatement();
            averageStat.execute("CREATE ALIAS AVERAGE FOR \"Main.getColumnAverage\"");

            Statement gaussStat = conn.createStatement();
            gaussStat.execute("CREATE ALIAS GAUSS FOR \"Main.getGaussienne\"");

            Statement rgbImageStat = conn.createStatement();
            rgbImageStat.execute("CREATE ALIAS RGBIMAGE FOR \"Main.getRGBImage\"");

            PreparedStatement prep = conn.prepareStatement("SELECT * FROM RGBIMAGE(\'MyJavaProject/src/hand.jpg\')"); // Premiere
                                                                                                                      // requête
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                // System.out.println(rs.getInt(1) + "/" + rs.getInt(2) + "/" + rs.getInt(3) +
                // "/" + rs.getInt(4) + "/" + rs.getInt(5));
            }
            prep.close(); // Fermeture de la première requête

            /*
             * PreparedStatement prepGauss =
             * conn.prepareStatement("SELECT * FROM GAUSS(5)");
             * ResultSet rsGauss = prepGauss.executeQuery();
             * while (rsGauss.next()) {
             * System.out.println(rsGauss.getInt(1) + "/" + rsGauss.getInt(2) + "/" +
             * rsGauss.getFloat(3));
             * }
             * prepGauss.close();
             */

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        // Question 3
        /*
         * try (Connection conn = DriverManager.getConnection(connUrl, username,
         * password)) {
         * Statement s = conn.createStatement();
         * s.
         * execute("CREATE TABLE UNEIMAGE AS SELECT * FROM RGBIMAGE(\'MyJavaProject/src/hand.jpg\')"
         * );
         * s.
         * executeUpdate("UPDATE UNEIMAGE SET R = ROUND(0.3*R + 0.59*G + 0.11*B), G=ROUND(0.3*R + 0.59*G + 0.11*B),B=ROUND(0.3*R + 0.59*G + 0.11*B)"
         * );
         * ResultSet rs = s.executeQuery("SELECT * FROM UNEIMAGE");
         * BufferedImage image = ImageIO.read(new File("MyJavaProject\\src\\hand.jpg"));
         * int width = image.getWidth();
         * int height = image.getHeight();
         * BufferedImage newImage = new BufferedImage(width, height,
         * BufferedImage.TYPE_INT_RGB);
         * while (rs.next()) {
         * int couleur= rs.getInt(3)<<16 | rs.getInt(4)<<8 | rs.getInt(5);
         * newImage.setRGB(rs.getInt(1), rs.getInt(2), couleur);
         * }
         * ImageIO.write(newImage, "png", new File("niveauxdegris.png"));
         * } catch (Exception e) {
         * e.printStackTrace(System.err);
         * }
         */

        // Question 4
        /*
         * try (Connection conn = DriverManager.getConnection(connUrl, username,
         * password)) {
         * Statement s = conn.createStatement();
         * s.
         * execute("CREATE TABLE UNEIMAGE AS SELECT * FROM RGBIMAGE('MyJavaProject/src/building.png')"
         * );
         * PreparedStatement prep = conn
         * .prepareStatement("SELECT * FROM RGBIMAGE(\'MyJavaProject/src/building.png\')"
         * );
         * gaussian(conn, "UNEIMAGE", 11);
         * prep.close();
         * } catch (Exception e) {
         * e.printStackTrace(System.err);
         * }
         */

        // Question 7

        /*
         * try (Connection conn = DriverManager.getConnection(connUrl, username,
         * password)) {
         * Statement stat = conn.createStatement();
         * stat.execute("CREATE ALIAS MATRIX FOR \"Main.getMatrix\"");
         * stat.execute("CREATE ALIAS AVERAGE FOR \"Main.getColumnAverage\"");
         * stat.execute("CREATE ALIAS GAUSS FOR \"Main.getGaussienne\"");
         * stat.execute("CREATE ALIAS RGBIMAGE FOR \"Main.getRGBImage\"");
         * 
         * downscale(conn, "MyJavaProject/src/hand.jpg", "downscaled_image.png", 2);
         * } catch (Exception e) {
         * e.printStackTrace(System.err);
         * }
         */

        // Relancer sans avoir a supprimer le fichier a chaque fois
        File file = new File("h2database.mv.db");
        file.delete();

    }
}
