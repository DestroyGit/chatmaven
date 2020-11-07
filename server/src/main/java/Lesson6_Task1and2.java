public class Lesson6_Task1and2 {
    public int[] task1(int[] array) {
        int count = 0;
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] != 4) {
                count++;
            } else {
                break;
            }
            if (array.length == count) {
                throw new RuntimeException();
            }
        }
        int[] arr = new int[count];
        for (int i = array.length - count, j = 0; i < array.length; i++, j++) {
            arr[j] = array[i];
        }
        return arr;
    }

    public boolean task2(int[] array) {
        int countOne = 0;
        int countFour = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 4) {
                countFour++;
            }
            if (array[i] == 1) {
                countOne++;
            }
        }
        if (countFour+countOne != array.length || countOne == 0 || countFour == 0){
            return false;
        }
        return true;
    }


}
