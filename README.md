SIRS
====

Project created in the context of the course "Segurança Informática em Redes e Sistemas"

Primeiro instalar CYGWIN+GIT(Só interessa mesmo a parte de instalar, o resto está explicado mais abaixo): http://ccn.ucla.edu/wiki/index.php/Setting_Up_and_Using_Git#Installing_CygWin_.2B_GIT

How-to git crash course

RECOMENDO ESTE GUIA:

http://try.github.io/levels/1/challenges/1

O RESTO É SÓ O BÁSICO DO BÁSICO

Setup: Metam o vosso nome e o vosso email associado à conta no GitHUb https://help.github.com/articles/set-up-git

Para clonar o repositório têm de criar chaves ssh que provem que são voces a queres aceder ao repositorio: https://help.github.com/articles/generating-ssh-keys

Clonar o repositório: Isto cria um repositório local onde podemos efectuar alterações.

git clone git@github.com:andre-nunes/ia.git

Para guardar as alteraçôes deve-se:

primeiro ver o que foi alterado desde o último commit git status

Quando se criam ficheiros novos é preciso adicionar à lista de ficheiros para commit:

git add newfile.c

Para fazer commit:

git commit -a -m "mensagem de commit"

Depois do commit no repositório local é preciso "empurrar" as mudanças para o repositório central no GitHub: mas antes é importante ver se o repositório central foi actualizado por alguém do grupo: git pull origin

Para empurrar as mudanças: git push origin master

E é isto.

https://help.github.com/articles/generating-ssh-keys
