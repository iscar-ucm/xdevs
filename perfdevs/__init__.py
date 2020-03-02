import math
import logging
from io import StringIO

INFINITY = math.inf
PHASE_PASSIVE = "passive"
PHASE_ACTIVE = "active"

DEBUG_LEVEL = logging.DEBUG
loggers = dict()

def get_logger(name):
    if name in loggers:
        return loggers[name]
    else:
        logger = logging.getLogger(name)

        if DEBUG_LEVEL:
            logger.setLevel(DEBUG_LEVEL)
        else:
            logger.disabled = True

        loggers[name] = logger
        return logger

