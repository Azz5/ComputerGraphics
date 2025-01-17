//
// Created by Abdulaziz on 1/15/2025.
//

#include "Vector3.h"


Vector3::Vector3(const double x, const double y, const double z)
    : x(x), y(y), z(z) {}

double Vector3::dot(const Vector3& vector3) const {
    return x * vector3.x +y * vector3.y + z * vector3.z;
}

Vector3 Vector3::Subtract(const Vector3& vector3) const {
    return Vector3(x - vector3.x, y - vector3.y, z - vector3.z);
}

double Vector3::getX() const {
    return x;
}

double Vector3::getY() const {
   return y;
}

double Vector3::getZ() const {
   return z;
}


