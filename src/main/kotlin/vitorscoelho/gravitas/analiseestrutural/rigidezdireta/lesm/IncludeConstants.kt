package vitorscoelho.gravitas.analiseestrutural.rigidezdireta.lesm

/*
 * Global Constants
 * This file contains variables that have a global scope and store flags to
 * help to understand the LESM program.
 * It is necessary to include this file in every function that uses a global
 * constant.
 * @author Luiz Fernando Martha, Rafael Lopez Rangel and Pedro Cortez Lopes
 */

/*Types of analysis models*/
/**2D truss analysis*/
const val TRUSS2D_ANALYSIS = 0

/**2D frame analysis*/
const val FRAME2D_ANALYSIS = 1

/**grillage analysis*/
const val GRILLAGE_ANALYSIS = 2

/**3D truss analysis*/
const val TRUSS3D_ANALYSIS = 3

/**3D frame analysis*/
const val FRAME3D_ANALYSIS = 4

/*Types of elements*/
/**Navier (Euler-Bernoulli) beam element*/
const val MEMBER_NAVIER = 0

/**Timoshenko beam element*/
const val MEMBER_TIMOSHENKO = 1

/*Types of continuity conditions*/
/**hinged element end*/
const val HINGED_END = 0

/**continuous element end*/
const val CONTINUOUS_END = 1

/*Types of load directions*/
/**element load applied in global system*/
const val GLOBAL_LOAD = 0

/**element load applied in local system*/
const val LOCAL_LOAD = 1

///*Types of essential boundary conditions*/
///**fictitious rotation constraints*/
//const val FICTFIXED_DOF = -1
//
///**free degree of freedom*/
//const val FREE_DOF = 0
//
///**fixed degree of freedom*/
//const val FIXED_DOF = 1
//
///**degree of freedom partially constrained by spring*/
//const val SPRING_DOF = 2
/*Types of essential boundary conditions*/
enum class BoundaryConditionType {
    /**fictitious rotation constraints*/
    FICTFIXED_DOF,

    /**free degree of freedom*/
    FREE_DOF,

    /**fixed degree of freedom*/
    FIXED_DOF,

    /**degree of freedom partially constrained by spring*/
    SPRING_DOF,
    ;
}