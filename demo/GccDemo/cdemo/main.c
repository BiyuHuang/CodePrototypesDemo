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
    static char *kwlist[] = {"object", "default", 0};
    long size;
    
    return 1L;
}
void print(int *s)
{
    printf("=> ");
    printf("%d",*s);
    printf("\n");
}
int main(int argc, const char * argv[]) {
    // insert code here...
    printf("=> C: Hello, World!\n");
    long s = getsizeof();
    printf("=> getsizeof: %ld\n",s);
    long *ts = &s;
    print(ts);
    char *p;
    p = &OS_TYPE;
    printf("=> %s\n", p);
    return 0;
}
