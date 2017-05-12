package simulation;

public class Reverse {

	public static void main(String[] s) {

		LNode node1 = new LNode();
		node1.value = 21;

		LNode node2 = new LNode();
		node2.value = 22;

		LNode node3 = new LNode();
		node3.value = 2;

		LNode node4 = new LNode();
		node4.value = 1;

		node1.next = node2;
		node2.next = node3;
		node3.next = node4;
		node4.next = null;

		print(reverse(node1));

	}

	public static void print(LNode head) {

		String ret = "";
		while (head != null) {
			ret += head.value + ", ";
			head = head.next;
		}

		System.out.println(ret);
	}

	public static LNode reverse(LNode list) {

		LNode retList = list;
		
		//print(list);
		
		int length = 0;
		LNode listTmp = list;
		while (null != listTmp) {
			listTmp = listTmp.next;
			length++;
		}

		int middleIndex = (length % 2 == 0) ? length / 2 : (length - 1) / 2;

		LNode middle = null;

		int count = 0;

		while (null != list.next) {

			if (count == middleIndex) {
				middle = list;
				break;
			}
			list = list.next;
			count++;
		}

		if (middle == null || middle.next == null)
			return retList;
		
		LNode p1 = middle;
		LNode p2 = p1.next;
		middle.next = null;

		while (p1 != null && p2 != null) {
			LNode t = p2.next;
			p2.next = p1;

			p1 = p2;

			p2 = t;

		}
		
//		LNode retN = retList;
//		while (null != retN.next) {
//			retN = retN.next;
//		}
//		
//		retN.next = p1; 

		return retList;
	}

}
