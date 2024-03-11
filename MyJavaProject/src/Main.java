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
    public void runSQL(String fileName)
{
    // Tout nom de fichier relatif DOIT commencer par ./ avec H2, sinon le driver refuse la chaine

        String connUrl = "jdbc:h2:./h2database";
        String username = "sa";
        String password = "";

        // Le fichier créé va être "h2database.mv.db" dans le repertoire de l'application Java

        try (Connection conn = DriverManager.getConnection(connUrl, username, password))
        {
            ScriptRunner runner = new ScriptRunner(conn, true, true);
            FileReader file = new FileReader(fileName);
            runner.runScript(new BufferedReader(file));		

            System.out.println();

            try (Statement st = conn.createStatement()) {
                st.execute("SHUTDOWN");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            assertTrue(false);
        }
}

    private void assertTrue(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assertTrue'");
    }

    public static ResultSet getColumnAverage(Connection conn, int column) throws // Récupère la moyenn d'une colonne (TP3)
    SQLException{
        SimpleResultSet rs = new SimpleResultSet();
         
        CustomAverage customAverage = new CustomAverage();
        customAverage.add(column);

        rs.addColumn("AverageX", Types.INTEGER, 10, 0);
        //String url = conn.getMetaData().getURL();
        //if (url.equals("jdbc:columnlist:connection")) {
        // return rs;
        //}
        // Ajoute des lignes (rangees, occurrences de la table)
        rs.addRow(customAverage.getResult());
        return rs;
    }

    public static ResultSet getMatrix(Connection conn, Integer size) throws
        SQLException {
            SimpleResultSet rs = new SimpleResultSet();
            rs.addColumn("X", Types.INTEGER, 10, 0);
            rs.addColumn("Y", Types.INTEGER, 10, 0);
            //String url = conn.getMetaData().getURL();
            //if (url.equals("jdbc:columnlist:connection")) {
            // return rs;
            //}
            // Ajoute des lignes (rangees, occurrences de la table)
            for (int s = size, x = 0; x < s; x++) {
                for (int y = 0; y < s; y++) {
                    rs.addRow(x, y);
                }
            }
            return rs;
        }


    // Tp6 Question 1
    public static ResultSet getGaussienne(Connection conn, Integer size) throws
        SQLException{
            SimpleResultSet rs = new SimpleResultSet();
            rs.addColumn("X", Types.INTEGER, 10, 0);
            rs.addColumn("Y", Types.INTEGER, 10, 0);
            rs.addColumn("VALEUR", Types.FLOAT, 10, 0);

            int indice = size/2;
            int depart = 0 - indice;

            for (int x = depart; x <= indice; x++) {
                    rs.addRow(x, x); 
            }
            return rs;
        }

    public static ResultSet getRGBImage(Connection conn, String path) throws
        SQLException, IOException{

            SimpleResultSet rs = new SimpleResultSet();
            rs.addColumn("X",Types.INTEGER,10, 0);
            rs.addColumn("Y",Types.INTEGER,10, 0);
            rs.addColumn("R",Types.FLOAT,10, 0);
            rs.addColumn("G",Types.FLOAT,10, 0);
            rs.addColumn("B",Types.FLOAT,10, 0);

            BufferedImage image = ImageIO.read(new File(path));

            int width = image.getWidth();
            int height = image.getHeight();

            for(int y = 0; y < height; y++){
                for(int x = 0; x < width; x ++){
                    int color = image.getRGB(x, y);
                    int red = (color >> 16) & 0xFF;
                    int green = (color >> 8) & 0xFF;
                    int blue = color & 0xFF;
                    rs.addRow(x,y,red,green,blue);
                }
            }

            return rs;
        }

        public static void main(String[] args) {
            String connUrl = "jdbc:h2:./h2database";
            String username = "sa";
            String password = "";
            // Le fichier créé va être "h2database.mv.db" dans le repertoire de'application Java
            try (Connection conn = DriverManager.getConnection(connUrl, username,
              password)) {
              // Votre code ici, qui utilise 'conn',
              // qui cree des statements, execute des requetes etc.


              // Création des différents Statements (correspond aux fonctions personnalisées SQL)
              Statement stat = conn.createStatement();
              stat.execute("CREATE ALIAS MATRIX FOR \"Main.getMatrix\"");

              Statement averageStat = conn.createStatement();
              averageStat.execute("CREATE ALIAS AVERAGE FOR \"Main.getColumnAverage\"");

              Statement gaussStat = conn.createStatement();
              gaussStat.execute("CREATE ALIAS GAUSS FOR \"Main.getGaussienne\"");

              Statement rgbImageStat = conn.createStatement();
              rgbImageStat.execute("CREATE ALIAS RGBIMAGE FOR \"Main.getRGBImage\"");

              
              PreparedStatement prep = conn.prepareStatement("SELECT * FROM RGBIMAGE(\'MyJavaProject/src/hand.jpg\')"); // Premiere requête
              ResultSet rs = prep.executeQuery();
              while (rs.next()) {
                System.out.println(rs.getInt(1) + "/" + rs.getInt(2) + "/" + rs.getInt(3) + "/" + rs.getInt(4) + "/" + rs.getInt(5));
              }
              prep.close(); // Fermeture de la première requête

              /*
              PreparedStatement prepAverage = conn.prepareStatement("SELECT AVERAGE(X) FROM MATRIX(2)"); // Deuxième requête 
              ResultSet rsAverage = prepAverage.executeQuery();
              while (rsAverage.next()) {
                System.out.println("Moyenne de X : " + rsAverage.getInt(1));
              }
              prepAverage.close(); // Fermeture de la deuxième requête
              */

            } catch (Exception e) {
              e.printStackTrace(System.err);
            }

            // Relancer sans avoir a supprimer le fichier a chaque fois 
            File file = new File("h2database.mv.db");
            file.delete();
          }
}
    


