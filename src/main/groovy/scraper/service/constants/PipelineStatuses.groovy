package scraper.service.constants

class PipelineStatuses {

    public static NOT_RUNNING = 'not running'
    public static PENDING = 'pending'
    public static QUEUE = 'queue' // in rabbitmq queue
    public static RUNNING = 'running'
    public static COMPLETED = 'completed'
    public static ERROR = 'error'
    public static STOPPING = 'stopping'
    public static STOPPED = 'stopped'
    public static WAIT_OTHER_PIPELINE = 'wait_other_pipeline'
}
