//
//  main.c
//  cdemo
//
//  Created by 黄碧宇 on 2019/9/19.
//  Copyright © 2019 黄碧宇. All rights reserved.
//

#include <stdio.h>

long getsizeof()
{
    return 1L;
}

int main(int argc, const char * argv[]) {
    // insert code here...
    printf("C: Hello, World!\n");
    long s = getsizeof();
    printf("getsizeof: %d\n",s);
    return 0;
}
