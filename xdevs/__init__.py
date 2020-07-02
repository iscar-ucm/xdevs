import math
import logging
import sys

INFINITY = math.inf
PHASE_PASSIVE = "passive"
PHASE_ACTIVE = "active"

DEBUG_LEVEL = None
loggers = dict()

def get_logger(name):
    raise RuntimeError()
    if name in loggers:
        return loggers[name]
    else:
        logger = logging.getLogger(name)

        if DEBUG_LEVEL:
            handler = logging.StreamHandler(sys.stdout)
            handler.setLevel(DEBUG_LEVEL)
            logger.addHandler(handler)
            logger.setLevel(logging.DEBUG)
        else:
            logger.disabled = True

        loggers[name] = logger
        return logger

