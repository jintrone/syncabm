package edu.msu.mi.syncabm

/**
 * Created by josh on 5/24/17.
 */
interface RelationshipFactory {

    public static RelationshipFactory singleObserverRelationshipFactory = { Collection<Agent> a->
        Agent observer = null
        List<Agent> generators = []

        a.each {
            if (it.parameters.policy=="OBSERVER") {
              if (observer != null) {
                  println "Factory only supports a single observer "
                  System.exit(-1)
              }
                observer = it

            } else if (it.parameters.policy == "GENERATOR") {
                generators<<it

            } else {
                println "Unknown policy, ignoring agent"
            }
        }
        [(observer):generators]


    } as RelationshipFactory



    public Map<Agent,Map<Agent,Integer>> wireAgents(Collection<Agent> a)




}
