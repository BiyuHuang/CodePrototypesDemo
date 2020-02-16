//
//  main.c
//  cdemo
//
//  Created by 黄碧宇 on 2019/9/19.
//  Copyright © 2019 黄碧宇. All rights reserved.
//

#include <stdio.h>
#ifndef OS_TYPE
#define OS_TYPE "macos"
#endif

long getsizeof()
{
    static char *kwlist[] = {"AAA", "BBB", 0};
    auto long size;
    size = sizeof(kwlist);
    return size;
}
void print(long *s)
{
    printf("=> ");
    printf("%ld",*s);
    printf("\n");
}
int main(int argc, const char * argv[]) {
    // insert code here...
    printf("=> C: Hello, World!\n");
    long s = getsizeof();
    printf("=> getsizeof: %ld Bytes\n",s);
    printf("=> OS Name: %s\n", OS_TYPE);
    
    int var1;
    char var2[10];
    printf("=> address of var1: %p\n", &var1);
    printf("=> address of var2: %p\n", &var2);
    
    /*
     指针是一个变量，其值为另一个变量的地址，即内存位置的直接地址。
     type *var-name;
     */
    char *ch;
    return 0;
}
