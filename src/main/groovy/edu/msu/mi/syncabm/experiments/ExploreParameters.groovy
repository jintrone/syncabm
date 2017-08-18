package edu.msu.mi.syncabm.experiments

import edu.msu.mi.syncabm.Context
import edu.msu.mi.syncabm.RelationshipFactory
import edu.msu.mi.syncabm.SingleLayerAgent
import edu.msu.mi.syncabm.inspectors.ObservationSimilarityInspector
import edu.msu.mi.syncabm.inspectors.SimilarityInspector
import edu.msu.mi.syncabm.inspectors.StateObservationInspector

/**
 * Created by josh on 6/2/17.
 */
class ExploreParameters {

    public static void main(String[] args) {

        Context c = new Context(SingleLayerAgent.factory, RelationshipFactory.singleObserverRelationshipFactory)
        (8..12).each { n ->
            (2..6).each { k ->
                (1..10).each { trial ->
                    c.prepareStudy(2, [[policy: "OBSERVER", ksize: k, nsize: n],
                                       [policy: "GENERATOR", ksize: k, nsize: n]])


                    c.agents[0].addInspector(new ObservationSimilarityInspector(c.agents[1], 500))
                    def stop = { c.agents[0].inspectors[0].lastSimilarity == 1.0 }
                    c.runStudy(200000, "behavioral_output.csv", trial, stop)
                    println("Done trial : $trial")
                }
            }
        }


    }
}
