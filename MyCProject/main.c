#include <locale.h>
#include <stdio.h>

int main(int argc, char **argv)
{
	char *str = setlocale(LC_ALL, NULL);
	printf("La locale courante est %s\n", str);
	printf("Avec cette locale, le nombre 12345678,8 est affiche comme suit: %1.2f\n", 1234567.8f);
	return 0;
}
