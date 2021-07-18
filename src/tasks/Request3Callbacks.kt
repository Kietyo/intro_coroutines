package tasks

import contributors.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

fun loadContributorsCallbacks(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    val allUsers = mutableListOf<User>()
    val call = service.getOrgReposCall(req.org)
    call.onResponse { responseRepos ->
        logRepos(req, responseRepos)
        val repos = responseRepos.bodyList()
        val atomicInteger = AtomicInteger()
        for ((i, repo) in repos.withIndex()) {
            val repoCall = service.getRepoContributorsCall(req.org, repo.name)
            repoCall.onResponse { responseUsers ->
                logUsers(repo, responseUsers)
                val users = responseUsers.bodyList()
                allUsers += users
                if (atomicInteger.incrementAndGet() == repos.size) {
                    updateResults(allUsers.aggregate())
                }
            }
        }
    }
}

inline fun <T> Call<T>.onResponse(crossinline callback: (Response<T>) -> Unit) {
    return enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}
