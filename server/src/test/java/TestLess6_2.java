import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// Тесты для задания 2

public class TestLess6_2 {
    Lesson6_Task1and2 tasks;

    @Before
    public void prepare(){
        tasks = new Lesson6_Task1and2();
    }


    @Test
    public void testTask2_1() {
        int[] array1 = {1, 4, 1, 4, 4, 4};
        Assert.assertTrue(tasks.task2(array1));
    }
    @Test
    public void testTask2_2() {
        int[] array2 = {1, 1, 1, 1, 1, 1};
        Assert.assertFalse(tasks.task2(array2));
    }
    @Test
    public void testTask_3() {
        int[] array3 = {1, 4, 3, 4, 4, 4};
        Assert.assertFalse(tasks.task2(array3));
    }
}
