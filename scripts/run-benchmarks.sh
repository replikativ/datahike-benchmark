
# Do once:
#
# 1) Install docker and docker-compose for non-root user
#
# sudo curl -sSL https://get.docker.com/ | sh
# sudo apt-get update && sudo apt-get upgrade
# sudo usermod -aG docker bench
#
# sudo curl -L https://github.com/docker/compose/releases/download/1.26.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
# sudo chmod +x /usr/local/bin/docker-compose
#
# 2) Install git and clone directory
# sudo apt update
# sudo apt install git
# git clone https://github.com/replikativ/datahike-benchmark.git
#
# 3) Log out and back in to activate new docker user
#
# 4) Create docker volumes
# docker volume create --name=dh-benchmark-db
# docker volume create --name=dh-benchmark-plots
# docker volume create --name=dh-benchmark-errors
# docker volume create --name=dh-benchmark-presentation

cd datahike-benchmark || exit

git pull
docker-compose build
docker-compose up --force-recreate

cd ..
