//
// Created by hp on 25/02/2023.
//

#include "Eigen/Dense"
void multiply2Matrices(){
    Eigen::MatrixXd M(2,2);
    Eigen::MatrixXd V(2,2);
    for (int i = 0;  i<=1; i++){
        for (int j = 0; j<=1; j++){
            M(i,j) = 1;
            V(i,j) = 2;
        }
    }
    Eigen::MatrixXd Result = M*V;
}