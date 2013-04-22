def execute_async(func):
    print "Executing [" + func.name + "] Asynchronously..."
    result = func.delay()
    while result.ready() != True:
        print "Waiting 0.5 secs for a result..."
        import time
        time.sleep(0.5)
    print "Got a result!"
    print "Result was [" + str(result.info) + "]"
