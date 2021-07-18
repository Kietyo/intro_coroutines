package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: listOf()

        val channels = Channel<List<User>>()

        for (repo in repos) {
            launch {
                channels.send(
                    service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }
                        .bodyList())
            }
        }

        var allUsers = emptyList<User>()

        repeat(repos.size) {
            allUsers = allUsers + channels.receive()
            updateResults(allUsers.aggregate(), it == repos.size - 1)
        }
    }
}
