package utils;

public enum AlgType {
	FLOWBASED(9999), 
	MULTIPLEXING(8888), 
	NOMULTIPLEXING(7777), 
	LIMITEDMULTIPLEXING(1111), 
	RANDOMTASKS(2222), 
	GOOGLETASKS(3333), 
	FLOWBASEDGREEDY(4444), 
	MULTIPLEDELAY(5555), 
	SINGLEDELAY(6666), 
	REQUNIFORM(11111), 
	REQRANDOM(11112), 
	REQGEOMETRIC(11113), 
	HETEROHEURISTIC(11114) // heuristic algorithm in heterogeneous settings
	;
	
	
	private int value;
	
	private AlgType(int value){
		this.value = value;
	}
}