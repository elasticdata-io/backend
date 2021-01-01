package scraper.constants

class PipelineStatuses {

    public static NOT_RUNNING = 'not running'
    public static NEED_USER_CONFIRMATION = 'need_user_confirmation'
    public static PENDING = 'pending'
    public static NEED_DEPS = 'need_other_pipeline'
    public static WAIT_DEPS = 'wait_other_pipeline'
    public static NEED_RUN = 'need_run'
    public static QUEUE = 'queue' // in rabbitmq queue
    public static RUNNING = 'running'
    public static COMPLETED = 'completed'
    public static ERROR = 'error'
    public static STOPPING = 'stopping'
    public static STOPPING_QUEUE = 'stopping_queue'
    public static STOPPED = 'stopped'

    static List<String> getInProcessing() {
        return [
            NEED_DEPS,
            NEED_RUN,
            PENDING,
            QUEUE,
            RUNNING,
            WAIT_DEPS,
        ]
    }

    static Boolean isTaskSuspended(String status) {
        return COMPLETED == status || ERROR == status || STOPPING == status || STOPPED == status
    }
}
