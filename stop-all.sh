#!/bin/bash

(cd devel/clients && docker-compose stop)
(cd devel/clients && docker-compose rm -f)

(cd devel/cluster && docker-compose stop)
(cd devel/cluster && docker-compose rm -f)