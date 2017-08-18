package edu.msu.mi.syncabm.inspectors

import edu.msu.mi.syncabm.Agent
import edu.msu.mi.syncabm.Inspector

/**
 * Created by josh on 6/2/17.
 */
class SimilarityInspector extends Inspector.BaseInspector {


    Agent other
    Map data
    int interval
    float lastSimilarity = 0.0

    List<String> header = ["similarity"]

    public SimilarityInspector(Agent other, int interval) {
        this.other = other
        this.interval = interval
    }

    @Override
    def afterAdvance(int step, Agent a) {
        if (data) {
            data = null
        }
        if (step % interval == 0) {
            data = [similarity: a.compareTo(other)]
            lastSimilarity = data.similarity
        }
    }

    boolean hasData() {
        data
    }


}
