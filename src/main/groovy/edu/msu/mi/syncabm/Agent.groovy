package edu.msu.mi.syncabm

/**
 * Created by josh on 5/24/17.
 */
interface Agent {

    Map getParameters()

    void addInspector(Inspector i)

    List<Inspector> getInspectors()

    void init()

    void observe(int step)

    void step(int i)

    State getState()

    Float compareTo(Agent agent)
}