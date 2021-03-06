package filter;

import java.util.Arrays;
import landscape.Landscape;
import utils.Matrix;

public class MidpointFilter extends LandFilter<Integer, Integer> {

    protected double R;
    protected Matrix<Integer> mx = new Matrix<>();
    protected Setter<Integer> addColumn = (a, b, c) -> mx.addColumn(a, b, c);
    protected Setter<Integer> addLine = (a, b, c) -> mx.addLine(a, b, c);
    protected Setter<Integer> set = (a, b, c) -> mx.set(a, b, c);
    protected Getter<Integer> getNDown = (a, b) -> mx.get(a, b);
    protected Getter<Integer> getModDown = (a, b) -> mx.get(a+1, b);
    

    public MidpointFilter(int seed, Getter<Integer> getter, Setter<Integer> setter) {
        super(seed, getter, setter);
        R = 0.5;
    }

    public static Getter<Integer> constructGetter(Landscape land, Getter<Integer> getter) {
        return (a, b) -> {
            if (a > -1 && b > -1) {
                return getter.get(a, b);
            }
            if (a == -1 && b == 0) {
                return land.getWidth();
            }
            if (a == -1 && b == -1) {
                return land.getHeight();
            }
            return null;
        };
    }

    @Override
    public void transform() {
        int width = getter.get(-1, 0);
        int height = getter.get(-1, -1);
        int points = 4;
        mx.add(getter.get(0, 0));
        mx.add(getter.get(height - 1, width - 1));
        mx.set(0, 1, getter.get(0, width - 1));
        mx.set(1, 0, getter.get(height - 1, 0));
        int l = (width + height) / 2;
        int hCons = 2;
        int wCons = 2;
        Setter<Integer> firstPoint;
        Setter<Integer> secondPoint;
        Setter<Integer> other = set;
        Getter<Integer> getDown; //Необходимо для поправки на появившуюся строчку

        while (points < width * height) {
            int a = 0;
            int b = 0;
            hCons = hCons * 2 - 1;
            wCons = wCons * 2 - 1;
            while (a < hCons - 1) {
                b = 0;
                while (b < wCons - 1) {
                    if (a == 0) {
                        firstPoint = addColumn;
                    } else {
                        firstPoint = set;
                    }
                    if (b == 0) {
                        secondPoint = addLine;
                        getDown = getNDown;
                    } else {
                        secondPoint = set;
                        getDown = getModDown;
                    }
                    
                    int h = mx.get(a, b) + mx.get(a, b + 1);
                    h /= 2;
                    firstPoint.set(a, b + 1, h);//point 1
                    points++;

                    h = mx.get(a, b + 2) + getDown.get(a + 1, b + 2);
                    h /= 2;
                    secondPoint.set(a + 1, b + 2, h);//point 2
                    points++;

                    h = mx.get(a + 2, b + 2) + mx.get(a + 2, b);
                    h /= 2;
                    other.set(a + 2, b + 1, h);//point 3
                    points++;

                    h = mx.get(a, b) + mx.get(a + 2, b);
                    h /= 2;
                    other.set(a + 1, b, h);//point 4
                    points++;

                    h = mx.get(a, b) + mx.get(a, b + 2) + mx.get(a + 2, b + 2) + mx.get(a + 2, b);
                    h /= 4;
                    int noise = r.nextInt((int) (R * l * 2 + 1)) - (int) (R * l * +1);
                    h += noise;
                    other.set(a + 1, b + 1, h);//point 5 - center noisy point
                    points++;

                    b += 2;//move the counting square left;
                }
                a += 2;//move the counting square down;
            }
            l /= 2;
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                setter.set(i, j, mx.get(i, j));
            }
        }
    }

    public double getRoughness() {
        return R;
    }

    public void setRoughness(double R) {
        this.R = R;
    }

}
