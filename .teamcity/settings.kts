import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_1.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.1"

project {
    description = "Contains all other projects"

    features {
        feature {
            id = "PROJECT_EXT_1"
            type = "ReportTab"
            param("startPage", "coverage.zip!index.html")
            param("title", "Code Coverage")
            param("type", "BuildReportTab")
        }
        feature {
            id = "PROJECT_EXT_2"
            type = "OAuthProvider"
            param("clientId", "2e3db557bb19ca24fc35")
            param("defaultTokenScope", "public_repo,repo,repo:status,write:repo_hook")
            param("secure:clientSecret", "zxx5c662730e9de4e494da6eb84f1633273595a19a03a0fb07c8f7913b0545acef752942c5bdd44d0c7775d03cbe80d301b")
            param("displayName", "GitHub.com")
            param("gitHubUrl", "https://github.com/")
            param("providerType", "GitHub")
        }
    }

    cleanup {
        preventDependencyCleanup = false
    }

    subProject(ScraperService)
}


object ScraperService : Project({
    name = "Scraper Service"
    description = "Contains all other projects"

    vcsRoot(ScraperService_ScraperServiceUi)
    vcsRoot(ScraperService_HttpsGithubComSergeytkachenkoScraperServiceRefsHeadsMaster)
    vcsRoot(ScraperService_ScraperCore)

    buildType(ScraperService_DockerBuild)
    buildType(ScraperService_FrontendDockerBuild)

    features {
        feature {
            id = "PROJECT_EXT_1"
            type = "ReportTab"
            param("startPage", "coverage.zip!index.html")
            param("title", "Code Coverage")
            param("type", "BuildReportTab")
        }
        feature {
            id = "PROJECT_EXT_2"
            type = "OAuthProvider"
            param("clientId", "e20b1dd602b1f34a20dd")
            param("defaultTokenScope", "public_repo,repo,repo:status,write:repo_hook")
            param("secure:clientSecret", "credentialsJSON:4cd161d5-9521-4973-b4c4-564675cab4ef")
            param("displayName", "GitHub.com")
            param("gitHubUrl", "https://github.com/")
            param("providerType", "GitHub")
        }
        versionedSettings {
            id = "PROJECT_EXT_3"
        }
    }

    cleanup {
        preventDependencyCleanup = false
    }
})

object ScraperService_DockerBuild : BuildType({
    name = "scraper service docker build"

    vcs {
        root(ScraperService_HttpsGithubComSergeytkachenkoScraperServiceRefsHeadsMaster)
        root(ScraperService_ScraperCore, "+:. => scraper-core")
    }

    steps {
        script {
            name = "docker build"
            scriptContent = "sh docker/buildAndPublish.sh"
        }
    }

    triggers {
        vcs {
            branchFilter = ""
        }
    }
})

object ScraperService_FrontendDockerBuild : BuildType({
    name = "frontend docker build"

    vcs {
        root(ScraperService_ScraperServiceUi)
    }

    steps {
        script {
            name = "rm old image"
            scriptContent = "docker image rm -f bombascter/scraper-service-ui"
        }
        script {
            name = "docker build"
            executionMode = BuildStep.ExecutionMode.ALWAYS
            scriptContent = "sh docker/build-and-publish.sh"
        }
    }

    triggers {
        vcs {
            branchFilter = ""
        }
    }
})

object ScraperService_HttpsGithubComSergeytkachenkoScraperServiceRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/sergeytkachenko/scraper-service#refs/heads/master"
    url = "https://github.com/sergeytkachenko/scraper-service"
    authMethod = password {
        userName = "sergeytkachenko"
        password = "zxx92f6c0a65afd1b5813686530571b64e2558fcea802b887df680d77a56a0cf93bf01244d58d103eee775d03cbe80d301b"
    }
})

object ScraperService_ScraperCore : GitVcsRoot({
    name = "scraper-core"
    url = "https://github.com/sergeytkachenko/scraper-core"
    authMethod = password {
        userName = "sergeytkachenko"
        password = "zxxdceefe22550305cc25803215f2bba88d"
    }
})

object ScraperService_ScraperServiceUi : GitVcsRoot({
    name = "scraper-service-ui"
    url = "https://github.com/sergeytkachenko/scraper-service-ui"
    authMethod = password {
        userName = "serg.tkachenko@hotmail.com"
        password = "zxxdceefe22550305cc25803215f2bba88d"
    }
})
