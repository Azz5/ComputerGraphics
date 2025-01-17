#include <float.h>
#include <iostream>
#include <math.h>
#include <valarray>

#include "Vector3.h"

const int Cw = 300 ;
const int Ch = 300 ;
const auto BACKGROUND_COLOR = Vector3(255,255,255) ;
const auto origin =  Vector3(0,0,0) ;

struct Sphere {
    Vector3 center;
    float radius{};
    Vector3 color;
};

struct Tdata {
    double t1;
    double t2;
};

Sphere spheres[] = {
    Sphere(Vector3(0, -1, 3), 1,Vector3(255, 0, 0) ),
    Sphere(Vector3(2, 0, 4), 1,Vector3(0, 0, 255) ),
    Sphere(Vector3(-2, 0, 4), 1,Vector3(0, 255, 0) ),
};

Vector3 CanvasToViewport(int x, int y) {
    double Vw = 1.0; // Viewport width in 3D space
    double Vh = 1.0; // Viewport height in 3D space
    double d = 1.0;  // Distance from the canvas to the viewport
    return Vector3((x * Vw) / Cw, (y * Vh) / Ch, d);
}


Tdata intersectRaySphere(const Vector3 & origin, const Vector3 & d, const Sphere & sphere) {
    Tdata data;
    double r = sphere.radius;
    Vector3 CO = origin.Subtract(sphere.center);

    double a = d.dot(d);
    double b = 2 * CO.dot(d);
    double c = CO.dot(CO) - r * r;

    double discriminant = b * b - 4 * a * c;

    if (discriminant < 0) {
        data.t1 = DBL_MAX;
        data.t2 = DBL_MAX;
        return data;
    }
    else {
        double sqrtDiscriminant = sqrt(discriminant);
        data.t1 = (-b + sqrtDiscriminant) / (2 * a);
        data.t2 = (-b - sqrtDiscriminant) / (2 * a);
    }

    return data;
};

Vector3 traceRay(Vector3 origin, Vector3 vector3, double t_min, double t_max) {
    double closest_t = DBL_MAX;
    Sphere closest_Sphere;
    bool hit_anything = false;
    for (Sphere sphere : spheres) {
        Tdata data = intersectRaySphere(origin,vector3,sphere);
        double t1 = data.t1;
        double t2 = data.t2;
        if (t_min < t1 && t1 < t_max && t1 < closest_t) {
            closest_t = t1;
            closest_Sphere = sphere;
            hit_anything = true;
        }
        if (t_min < t2 && t2 < t_max && t2 < closest_t) {
            closest_t = t2;
            closest_Sphere = sphere;
            hit_anything = true;
        }
    }
    if (!hit_anything) {
        return BACKGROUND_COLOR;
    }
    return closest_Sphere.color;
};

int main() {

    std::cout << "P3\n" << Cw+1 << ' ' << Ch+1 << "\n255\n";

    for (int x = -Cw / 2; x < Cw / 2; x++) {
        for (int y = -Ch / 2; y < Ch / 2; y++) {
            Vector3 directionVector = CanvasToViewport(x, y);
            Vector3 color = traceRay(origin, directionVector, 1.0, DBL_MAX);
            //int px = (Cw / 2) + x;
            //int py = (Ch / 2) - y;
            std::cout << color.getX() << ' ' << color.getY() << ' ' << color.getZ()<< '\n';


        }
    }
    return 0;
}
