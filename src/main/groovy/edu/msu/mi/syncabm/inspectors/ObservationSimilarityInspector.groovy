package edu.msu.mi.syncabm.inspectors

import edu.msu.mi.syncabm.Agent
import edu.msu.mi.syncabm.Inspector

/**
 * Created by josh on 6/2/17.
 */
class ObservationSimilarityInspector extends Inspector.BaseInspector {

    Set observedStatespace = [] as Set

    Map data
    List header = ["n","k","observed"]
    int interval
    float lastSimilarity = 0.0
    Agent other

    public ObservationSimilarityInspector(Agent other,int interval) {
        this.other = other
        this.interval = interval
    }

    @Override
    def afterObservation(int step, Agent a) {
        if (!a.observations.isEmpty()) {
            observedStatespace << a.observations[0].behavior
        }

        if (data!=null) data = null

        if (step % interval == 0) {
            lastSimilarity = a.compareTo(other)
            if (lastSimilarity == 1.0) {
                data =[n:a.parameters.nsize,k:a.parameters.ksize,observed: observedStatespace.size() / 2**a.parameters.nsize]
            }

        }
    }


    @Override
    boolean hasData() {
        data
    }
}

