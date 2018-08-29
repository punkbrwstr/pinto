package tech.pinto.tools;

import java.util.concurrent.atomic.AtomicInteger;

public class ID {
	
	private static final AtomicInteger current = new AtomicInteger();

	public static String getId() {
		return toBase26(current.getAndIncrement());
	}

    private static String toBase26(int i){
        String s = "";
        while(i > 25) {
            int r = i % 26;
            i = i / 26;
            s = (char)(r + 65) + s;//26 + 64 = 100
        }
        s = (char)(i + 65) + s;
        return s.toLowerCase();
    }

}
