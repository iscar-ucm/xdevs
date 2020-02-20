class NewClient:
    def __init__(self, client_id, t_entered):
        self.client_id = client_id
        self.t_entered = t_entered


class ClientToEmployee:
    def __init__(self, new_client, employee_id):
        self.client = new_client
        self.employee_id = employee_id


class LeavingClient:
    def __init__(self, client_id, t_entered, t_exited):
        self.client_id = client_id
        self.t_entered = t_entered
        self.t_exited = t_exited
