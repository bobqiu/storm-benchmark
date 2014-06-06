package storm.benchmark.component.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;
import storm.benchmark.component.bolt.common.RollingBolt;
import storm.benchmark.lib.reducer.LongSummer;
import storm.benchmark.util.SlidingWindow;

import java.util.Map;
import java.util.Map.Entry;

/**
 * forked from RollingCountBolt in storm-starter
 */

public class RollingCountBolt extends RollingBolt {

  private static final long serialVersionUID = -903093673694769540L;
  private static final Logger LOG = Logger.getLogger(RollingCountBolt.class);
  public static final String FIELDS_OBJ = "obj";
  public static final String FIELDS_CNT = "count";

  private final SlidingWindow<Object, Long> window;

  public RollingCountBolt() {
    this(DEFAULT_SLIDING_WINDOW_IN_SECONDS, DEFAULT_EMIT_FREQUENCY_IN_SECONDS);
  }

  public RollingCountBolt(int winLen, int emitFreq) {
    super(winLen, emitFreq);
    window = new SlidingWindow<Object, Long>(new LongSummer(), getWindowChunks());
  }

  @Override
  public void emitCurrentWindow(BasicOutputCollector collector) {
    emitCurrentWindowCounts(collector);
  }

  @Override
  public void updateCurrentWindow(Tuple tuple) {
    countObj(tuple);
  }

  private void emitCurrentWindowCounts(BasicOutputCollector collector) {
    Map<Object, Long> counts = window.reduceThenAdvanceWindow();
    for (Entry<Object, Long> entry : counts.entrySet()) {
      Object obj = entry.getKey();
      Long count = entry.getValue();
      LOG.debug("Object: " + obj + ", Count: " + count);
      collector.emit(new Values(obj, count));
    }
  }

  private void countObj(Tuple tuple) {
    Object obj = tuple.getValue(0);
    window.add(obj, (long) 1);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields(FIELDS_OBJ, FIELDS_CNT));
  }
}
