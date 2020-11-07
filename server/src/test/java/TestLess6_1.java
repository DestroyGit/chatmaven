import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/*
Тесты для задания 1
 */

@RunWith(Parameterized.class)
public class TestLess6_1 {
    Lesson6_Task1and2 tasks;
    private int [] array;
    private int [] arr;

    public TestLess6_1(int[] arr, int[] array) {
        this.arr = arr;
        this.array = array;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int[] arr1 = {5, 6};
        int[] array1 = {1, 2, 3, 4, 5, 6};

        int[] arr2 = {2, 3, 3, 5, 6};
        int[] array2 = {4, 2, 3, 3, 5, 6};

        int[] arr3 = {};
        int[] array3 = {1, 2, 3, 3, 5, 4};

        return Arrays.asList(new Object[][]{
                {arr1, array1},
                {arr2,array2},
                {arr3, array3}
        });
    }

    @Before
    public void init(){
        tasks = new Lesson6_Task1and2();
    }

    @Test
    public void testTask1_1() {
        Assert.assertArrayEquals(arr, tasks.task1(array));
    }

    @Test(expected = RuntimeException.class)
    public void testTask1_2() {
        int[] arr3 = {};
        int[] array3 = {1, 2, 3, 3, 5, 6};
        Assert.assertArrayEquals(arr3, tasks.task1(array3));
    }
}
