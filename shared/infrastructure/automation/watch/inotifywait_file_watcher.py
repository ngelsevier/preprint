import os
import string
import threading
from fabric.api import local
from fabric.context_managers import hide, settings
from time import sleep


class InotifywaitFileWatcher:
    def __init__(self, process_interrupted_event, debug=False):
        self.process_interrupted_event = process_interrupted_event
        self.suppressed_fabric_log_levels = [] if debug else ['running', 'warnings', 'output']

    def on_change_of_file_under(self, file_paths, handle_file_changed, log_message_prefix):
        def target():
            try:
                while True:
                    self.__wait_for_existence_of_all_host_file_paths_mounted_by(file_paths, log_message_prefix,
                                                                                handle_file_changed)
                    print '{log_message_prefix}Waiting for watched files to change...'.format(
                        log_message_prefix=log_message_prefix)

                    inotifywait_command = 'inotifywait ' \
                                          '--recursive ' \
                                          '--quiet ' \
                                          '--event modify ' \
                                          '--event attrib ' \
                                          '--event move ' \
                                          '--event move_self ' \
                                          '--event create ' \
                                          '--event delete ' \
                                          '--event delete_self '

                    with settings(hide(*self.suppressed_fabric_log_levels), warn_only=True):
                        while local(inotifywait_command + string.join(file_paths)).succeeded:
                            handle_file_changed()
            except:
                if not self.process_interrupted_event.is_set():
                    raise

        thread = threading.Thread(target=target)
        thread.setDaemon(True)
        thread.start()

    def wait_whilst_watching(self):
        while threading.active_count() > 1:
            sleep(0.1)

    def __wait_for_existence_of_all_host_file_paths_mounted_by(self, file_paths, log_message_prefix,
                                                               handle_host_file_paths_created):
        waited_at_least_once = False

        while not all(os.path.exists(mounted_host_file_path) for mounted_host_file_path in
                      file_paths):
            if not waited_at_least_once:
                print '{log_message_prefix}Waiting for all watched file paths to exist...'.format(
                    log_message_prefix=log_message_prefix)
                waited_at_least_once = True
            sleep(0.1)

        if waited_at_least_once:
            handle_host_file_paths_created()
