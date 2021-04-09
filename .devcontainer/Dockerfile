FROM ubuntu

MAINTAINER José Luis Risco Martín (jlrisco@ucm.es)

RUN apt-get clean
RUN apt-get update --fix-missing
RUN apt-get update
RUN sed -i 's:^path-exclude=/usr/share/man:#path-exclude=/usr/share/man:' /etc/dpkg/dpkg.cfg.d/excludes
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y locales sudo bash-completion whois time wget git default-jdk maven

# LOCALE
RUN sed -i '/es_ES.UTF-8/s/^# //g' /etc/locale.gen && locale-gen
ENV LANG es_ES.UTF-8
ENV LANGUAGE es_ES:es
ENV LC_ALL es_ES.UTF-8

# USUARIO
RUN useradd -m usuario -p sa5u2O0Xjsufg
RUN usermod -aG sudo usuario

USER usuario
WORKDIR /home/usuario
