package jzyu.github.com.seeddemo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: jzyu
 * Date  : 2017/5/18
 */

public class ApiData {
    public static class Movies {
        public List<MovieItem> subjects = new ArrayList<>();
    }

    public static class MovieItem {

        public RatingBean rating;
        public String title;
        public int collect_count;
        public String original_title;
        public String subtype;
        public String year;
        public ImagesBean images;
        public String alt;
        public String id;
        public List<String> genres;
        public List<CastsBean> casts;
        public List<DirectorsBean> directors;

        public static class RatingBean {
            public int max;
            public double average;
            public String stars;
            public int min;
        }

        public static class ImagesBean {
            public String small;
            public String large;
            public String medium;
        }

        public static class CastsBean {
            public String alt;
            public AvatarsBean avatars;
            public String name;
            public String id;

            public static class AvatarsBean {
                public String small;
                public String large;
                public String medium;
            }
        }

        public static class DirectorsBean {
            public String alt;
            public AvatarsBeanX avatars;
            public String name;
            public String id;

            public static class AvatarsBeanX {
                public String small;
                public String large;
                public String medium;
            }
        }
    }
}
