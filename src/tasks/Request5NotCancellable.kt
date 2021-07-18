package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val job = GlobalScope.async {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

        val deferreds = repos.map { repo -> async {
            service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }
                .bodyList()
        } }

        deferreds.awaitAll().flatten().aggregate()
    }
    return job.await()
}