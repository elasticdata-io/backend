package scraper.service.constants

class PipelineStatuses {

    public static NOT_RUNNING = 'not running'
    public static PENDING = 'pending'
    public static NEED_DEPS = 'need_other_pipeline'
    public static WAIT_DEPS = 'wait_other_pipeline'
    public static NEED_RUN = 'need_run'
    public static QUEUE = 'queue' // in rabbitmq queue
    public static RUNNING = 'running'
    public static COMPLETED = 'completed'
    public static ERROR = 'error'
    public static STOPPING = 'stopping'
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
}
