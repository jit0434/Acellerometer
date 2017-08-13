package com.example.papa.acceldataplot;

/**
 * Created by PAPA on 8/7/2017.
 */
/**
 * This interface represents types which are able to filter data, for example:
 * eliminate redundant points.
 *
 * @author Marcin Rzeźnicki
 * @see SGFilter#appendPreprocessor(Preprocessor)
 */
public interface DataFilter {

    double[] filter(double[] data);
}