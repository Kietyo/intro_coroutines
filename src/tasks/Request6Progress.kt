package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    var allUsers = emptyList<User>()

    repos.forEach { repo ->
        val users = service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
        allUsers = (allUsers + users)
        updateResults(allUsers.aggregate(), false)
    }

    updateResults(allUsers.aggregate(), true)
}
