package vitorscoelho.gravitas.analiseestrutural.rigidezdireta.lesm

/**
 * Defines node objects in the LESM (Linear Elements
 * Structure Model) program.
 *
 * A node is a joint between two or more elements, or any element end, used
 * to discrete the model. It is always considered as a tri-dimensional
 * entity which may have applied loads or prescribed displacements.
 * @author Luiz Fernando Martha, Rafael Lopez Rangel and Pedro Cortez Lopes
 * @property coord vector of coordinates on global system [X Y Z]
 * @property ebc vector of essential boundary condition flags [dx dy dz rx ry rz]: -1 = fictitious, 0 = free, 1 = fixed, 2 = spring
 * @property nodalLoadCase array of nodal loads and prescribed displacements for each load case [fx fy fz mx my mz dx dy dz rx ry rz]'
 * @property nodalLoad vector of applied load components [fx fy fz mx my mz]
 * @property prescDispl vector of prescribed displacement values [dx dy dz rx ry rz]
 * @property springStiff vector of spring stiffness coefficients [kx ky kz krx kry krz]
 */
class Node(
    val coord: Coordinates,
    val ebc: BoundaryCondition,
//    val nodalLoadCase = [],
    val nodalLoad: NodalLoad,
    val prescDispl: Displacement,
    val springStiff: SpringStiff,
)

/*
** Node Class
*

*
** Authors
*
%
%% Class definition
classdef Node < handle
    %% Public attributes
    properties (SetAccess = public, GetAccess = public)
        id = 0;               % identification number
        coord = [];           % vector of coordinates on global system [X Y Z]
        ebc = [];             % vector of essential boundary condition flags [dx dy dz rx ry rz]: -1 = fictitious, 0 = free, 1 = fixed, 2 = spring
        nodalLoadCase = [];   % array of nodal loads and prescribed displacements for each load case [fx fy fz mx my mz dx dy dz rx ry rz]'
        nodalLoad = [];       % vector of applied load components [fx fy fz mx my mz]
        prescDispl = [];      % vector of prescribed displacement values [dx dy dz rx ry rz]
        springStiff = [];     % vector of spring stiffness coefficients [kx ky kz krx kry krz]
    end

    %% Public methods
    methods
        %------------------------------------------------------------------
        % Counts total number of elements and number of hinged elements
        % connected to a node.
        % Output:
        %  tot: total number of elements connected to a node
        %  hng: number of hinged elements connected to a node
        % Input arguments:
        %  drv: handle to an object of the Drv class
        function [tot,hng] = elemsIncidence(node,drv)
            % Initialize values
            tot = 0;
            hng = 0;

            for e = 1:drv.nel
                % Check if initial node of current element is the target one
                if drv.elems(e).nodes(1).id == node.id
                    tot = tot + 1;
                    if drv.elems(e).hingei == 0
                        hng = hng + 1;
                    end

                % Check if final node of current element is the target one
                elseif drv.elems(e).nodes(2).id == node.id
                    tot = tot + 1;
                    if drv.elems(e).hingef == 0
                        hng = hng + 1;
                    end
                end
            end
        end
    end
end
 */