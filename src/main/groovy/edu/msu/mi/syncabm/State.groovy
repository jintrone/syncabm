package edu.msu.mi.syncabm

/**
 * Created by josh on 5/26/17.
 */
interface State {

    def getBehavior()
    Integer getStep()
    Map getMetaData()
}
