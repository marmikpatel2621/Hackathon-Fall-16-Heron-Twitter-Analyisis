package com.mnp.twittersource.bolts;

import java.util.HashMap;
import java.util.Map;
import backtype.storm.Config;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import com.mnp.herontweets.tools.Rankings;
import com.mnp.herontweets.util.TupleHelpers;

/**
 * This abstract bolt provides the basic behavior of bolts that rank objects according to their count.
 * 
 * It uses a template method design pattern for {@link AbstractRankerBolt#execute(Tuple, BasicOutputCollector)} to allow
 * actual bolt implementations to specify how incoming tuples are processed, i.e. how the objects embedded within those
 * tuples are retrieved and counted.
 * 
 */
public abstract class AbstractRankerBolt extends BaseBasicBolt {

    private static final long serialVersionUID = 4931640198501530202L;
    private static final int DEFAULT_EMIT_FREQUENCY_IN_SECONDS = 2;
    private static final int DEFAULT_COUNT = 10;

    private final int emitFrequencyInSeconds;
    private final int count;
    private final Rankings rankings;
    String src;

    public AbstractRankerBolt() {
        this(DEFAULT_COUNT, DEFAULT_EMIT_FREQUENCY_IN_SECONDS);
    }

    public AbstractRankerBolt(int topN) {
        this(topN, DEFAULT_EMIT_FREQUENCY_IN_SECONDS);
    }

    public AbstractRankerBolt(int topN, int emitFrequencyInSeconds) {
        if (topN < 1) {
            throw new IllegalArgumentException("topN must be >= 1 (you requested " + topN + ")");
        }
        if (emitFrequencyInSeconds < 1) {
            throw new IllegalArgumentException("The emit frequency must be >= 1 seconds (you requested "
                + emitFrequencyInSeconds + " seconds)");
        }
        count = topN;
        this.emitFrequencyInSeconds = emitFrequencyInSeconds;
        rankings = new Rankings(count);
    }

    protected Rankings getRankings() {
        return rankings;
    }

    /**
     * This method functions as a template method (design pattern).
     */
    public final void execute(Tuple tuple, BasicOutputCollector collector) {
    	// TODO Auto-generated method stub
    	if (TupleHelpers.isTickTuple(tuple)) {
    		System.out.println("Received tick tuple, triggering emit of current rankings");
            collector.setContext(tuple);
            emitRankings(collector);
        }
        else {
            updateRankingsWithTuple(tuple);
        }
    }

    abstract void updateRankingsWithTuple(Tuple tuple);
    
    abstract void emitRankings(BasicOutputCollector collector);
    
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    	// TODO Auto-generated method stub
    	declarer.declare(new Fields("rankings","src"));
    }

    public Map<String, Object> getComponentConfiguration() {
    	// TODO Auto-generated method stub
    	Map<String, Object> conf = new HashMap<String, Object>();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencyInSeconds);
        return conf;
    }
 }