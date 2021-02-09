package efp

import "fmt"

type Job struct {
	Id   string
	Time float64
}

func (j *Job) ToString() string {
	return "(id,t)=(" + j.Id + "," + fmt.Sprintf("%v", j.Time) + ")"
}
