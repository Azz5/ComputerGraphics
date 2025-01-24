//
// Created by Abdulaziz on 1/15/2025.
//

#ifndef VECTOR3_H
#define VECTOR3_H




class Vector3 {
    public:
        explicit Vector3(double x = 0, double y = 0, double z = 0);
        Vector3 Subtract(const Vector3 &vector3) const;
        double dot(const Vector3 &vector3) const;
        double getX() const;
        double getY() const;
        double getZ() const;
    private:
        double x , y, z;
};



#endif //VECTOR3_H
