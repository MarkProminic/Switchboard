import switchboard.cloud.Extension
import switchboard.cloud.ExtensionLocation
import switchboard.cloud.FollowMeNumber
import switchboard.cloud.RingGroup
import security.Agent

model {
    List<ExtensionLocation> options
    List<FollowMeNumber> followMeSteps
    Agent user
}
xmlDeclaration()
settings(name: user.name, id: user.id){

    nameOptions {
        options.each {
            option(prefix: it.prefix, name: it.name, sipAllowed: it.sipAllowed)
        }
    }

    ringGroups user.ringGroups, { RingGroup group ->
        id group.id
        name group.name
        extensions user.extensions.sort {a,b -> a.displayName <=> b.displayName }, { Extension extension ->
            id extension.id
            name extension.displayName
            active(extension in group.extensions)
        }
    }

    followme {
        steps g.render(template: '/followme/followmesteps', model:[followMeSteps: followMeSteps, user: user])
        elementList {
            extensions user.extensions.sort {a,b -> a.displayName <=> b.displayName }, { Extension extension ->
                id extension.id
                name extension.displayName
            }

            groups user.ringGroups.sort {a,b -> a.name <=> b.name }, { RingGroup group ->
                id group.id
                name group.name
            }
        }
    }
}