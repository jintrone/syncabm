package edu.msu.mi.syncabm

/**
 * Created by josh on 5/24/17.
 */
class HiddenNodesAgent extends SingleLayerAgent {


    public HiddenNodesAgent(Context context, Map params, int id) {
        super(context, params, id)
    }

    @Override
    public void init() {
        states << new State(u.getRandomBitSet(parameters.nsize), step, "INIT", parameters.vsize)
    }



    private BitSet decorateObservation(BitSet obs) {
        BitSet result = obs.clone()
        if (states.size() > 1) {
            (parameters.vsize..<parameters.nsize).each {
                result.set(it, states[states.size() - 2].metaData.full.get(it))
            }
        }
        result
    }

    //learn from one observation right now
    protected learnFromObservations() {

        if (!observations.isEmpty()) {
            BitSet obs = observations[0].behavior
            if (observations[0].metaData.statetype != "INIT") {
                //println("${step}. ${parameters.policy} attempting to learn from ${observations[0]}")


                //for now, let 's just find the first one that doesn' t match
                //println "${state.behavior} ?= ${obs}"
                Integer target = u.shuffle((0..<parameters.vsize).toList()).find {
                    state.behavior.get(it) != obs.get(it)
                }

                if (target != null) {
                    update(states[states.size() - 2].metaData.full, target, obs[target])

                } else {
                    println "${step} - No differences!"
                }
            } else {
               states.clear()
            }
        }



        if (parameters.policy == "OBSERVER") {
            //println("${step}. ${parameters.policy} copies ${observations[0]}")
            states << new State(decorateObservation(observations[0].behavior), step, "COPY",parameters.vsize)
        }

        observations.clear()

    }



    private void update(BitSet last, int idx, boolean desired) {
        Map nodefunction = connections.get(idx)
        int fxidx = getFxIndex(last, idx)
        LearningData ld = nodefunction.learn
        ld.update(fxidx, desired)


        if (ld.shouldRewire()) {
            //find the first output val at this node that matches the desired end state
            if (ld.rejected.size() == allcombinations.size()) {
                println "Exhausted combinations, reset"
                ld.rejected.clear()
            }
            List candidates = (allcombinations - ld.rejected).toList()
            nodefunction.connections = candidates[u.rand.nextInt(candidates.size())]
            ld.rewire(nodefunction.connections)

        } else {

            nodefunction.fx.set(fxidx, desired)
        }


    }



    protected advanceState() {


        if (parameters?.policy == "GENERATOR" && states.size() > 1) {
            //println("${step}. ${parameters.policy} checking for attractor")
            //check to see if we're in an attractor; if so, re-init
            if (states.subList(states.findLastIndexOf { it.metaData.statetype == "INIT" }, states.size() - 1).find {
                it.metaData.full == state.metaData.full
            }) {
                states.clear()
                states << new State(u.getRandomBitSet(parameters.nsize), ++step, "INIT",parameters.vsize)
                //println("${step}. ${parameters.policy} found attractor and updating: $state")
                return
            }
        }

        // either we're not in an attractor, or we're not a generator

        //println("${step}. ${parameters.policy} generating from: $state.behavior")
        def nstate = new State(calculateAdvance(connections, state.metaData.full), ++step, "GENERATE", parameters.vsize)

        states << nstate


    }

    private static BitSet calculateAdvance(List<Map> network, BitSet probe) {

        BitSet result = new BitSet()
        network.eachWithIndex { Map obj, int idx ->
            int order = 0
            if (obj.fx.get(obj.connections.sum { f ->
                (probe.get(f) ? 1 : 0) * 2**order++
            })) result.set(idx)
        }
        result
    }


    public static class State implements edu.msu.mi.syncabm.State {


        BitSet behavior
        Integer step
        Map metaData

        public State(BitSet fullstate, int step, String meta, int visible) {
            this.behavior = fullstate.get(0, visible)
            this.step = step
            this.metaData = [statetype: meta, full: fullstate]

        }

        public String toString() {
            "${behavior ?: "{no state}"}, step=$step, ${metaData}"
        }

    }

    public static AgentFactory getFactory() {
        { Context c, Map parameters, int id -> new HiddenNodesAgent(c, parameters, id) } as AgentFactory
    }

    public Float compareTo(Agent agent) {
        (0..<2**parameters.nsize).sum {
            calculateAdvance(connections, BitSet.valueOf([it] as long[])) ==
                    calculateAdvance(agent.connections, BitSet.valueOf([it] as long[])) ? 1 : 0
        } / (2**parameters.nsize)
    }

    public String toString() {
        return "HiddenNodesAgent.$id : ${state}"
    }


}
