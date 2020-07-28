import math
import logging
import sys

INFINITY = math.inf
PHASE_PASSIVE = "passive"
PHASE_ACTIVE = "active"

DEBUG_LEVEL = None
loggers = dict()

def get_logger(name, dl=None):
    if name in loggers:
        return loggers[name]
    else:
        logger = logging.getLogger(name)

        if dl or DEBUG_LEVEL:
            handler = logging.StreamHandler(sys.stdout)
            handler.setLevel(dl or DEBUG_LEVEL)
            logger.addHandler(handler)
            logger.setLevel(logging.DEBUG)
        else:
            logger.disabled = True

        loggers[name] = logger
        return logger
