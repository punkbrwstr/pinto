package tech.pinto.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;



public class LogAppender extends AppenderBase<ILoggingEvent> {
public static ArrayBlockingQueue<String> LOG = new  ArrayBlockingQueue<String>(1000);
  static int DEFAULT_LIMIT = 100000;
  int counter = 0;
  int limit = DEFAULT_LIMIT;
  
  PatternLayoutEncoder encoder;
  
  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }
  
  @Override
  public void start() {
    if (this.encoder == null) {
      addError("No encoder set for the appender named ["+ name +"].");
      return;
    }
    
    try {
      encoder.init(new OutputStream()
      {
          private StringBuilder string = new StringBuilder();
          @Override
          public void write(int b) throws IOException {
        	  if(((char)b) == '\n') {
        		  if(LOG.remainingCapacity() == 0) {
        			  LOG.remove();
        		  } else {
        			  LOG.add(string.toString());
        			  string.setLength(0);
        		  }
        	  } else {
        		  this.string.append((char) b );
        	  }
          }

          public String toString(){
              return this.string.toString();
          }
      });
    } catch (IOException e) {
    	e.printStackTrace();
    }
    super.start();
  }

  public void append(ILoggingEvent event) {
    if (counter >= limit) {
      return;
    }
    try {
      this.encoder.doEncode(event);
    } catch (IOException e) {
    	e.printStackTrace();
    }

    counter++;
  }

  public PatternLayoutEncoder getEncoder() {
    return encoder;
  }

  public void setEncoder(PatternLayoutEncoder encoder) {
    this.encoder = encoder;
  }
}
