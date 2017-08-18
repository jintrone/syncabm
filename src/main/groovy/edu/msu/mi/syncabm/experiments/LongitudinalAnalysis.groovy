package edu.msu.mi.syncabm.experiments

import edu.msu.mi.syncabm.Context
import edu.msu.mi.syncabm.RelationshipFactory
import edu.msu.mi.syncabm.SingleLayerAgent
import edu.msu.mi.syncabm.inspectors.SimilarityInspector
import edu.msu.mi.syncabm.inspectors.StateObservationInspector

/**
 * Created by josh on 6/2/17.
 */
class LongitudinalAnalysis {

    public static void main(String[] args) {

        Context c = new Context(SingleLayerAgent.factory, RelationshipFactory.singleObserverRelationshipFactory)


        (1..10).each { trial->
            c.prepareStudy(2, [[policy: "OBSERVER", ksize: 4, nsize: 12],
                    [policy: "GENERATOR", ksize: 4, nsize: 12]])


            c.agents[0].addInspector(new SimilarityInspector(c.agents[1], 500))
            c.agents[0].addInspector(new StateObservationInspector(500))

            def stop = {c.agents[0].inspectors[0].lastSimilarity==1.0}
            c.runStudy(100000, "longitudinal_output.csv",trial,stop)
            println("Done trial : $trial")
        }


    }

}
