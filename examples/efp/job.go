package efp

import "fmt"

type Job struct {
	Id   string
	Time float64
}

func (j *Job) ToString() string {
	return fmt.Sprintf("(id,t)=(%v,%v)", j.Id, j.Time)
}
