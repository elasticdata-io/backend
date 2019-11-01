package scraper.service.store

import scraper.service.model.Task

class TaskBucketObject {
    String bucketName
    String objectName

    static TaskBucketObject fromTask(Task task) {
        return new TaskBucketObject(bucketName: task.userId, objectName: "${task.id}.json")
    }
}
