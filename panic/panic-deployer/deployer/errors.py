__author__ = 'Giannis Giannakopoulos'

class ArgumentsError(Exception):
    def __init__(self, message):
        self.message = message
