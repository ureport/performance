#!/usr/bin/env pytho
from ureport.tasks import ping

def run():
    print "Ok, going to do something...."
    ping.delay()

